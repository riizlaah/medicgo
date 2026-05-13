using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/appointments")]
    [ApiController]
    public class AppointmentsController : ControllerBase
    {
        private readonly MedicGoContext dbc;
        public AppointmentsController(MedicGoContext ctx) { dbc = ctx; }

        [HttpGet]
        [Authorize]
        public IActionResult GetAll(string sort = "desc")
        {
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var data = dbc.Appointments.Include(d => d.Doctor).Where(a => a.PatientId == userId).AsQueryable();
            if(sort == "desc")
            {
                data = data.OrderByDescending(a => a.CreatedAt);
            } else
            {
                data = data.OrderBy(a => a.CreatedAt);
            }
            return Ok(new
            {
                data = data.Select(a => new
                {
                    id = a.Id,
                    doctorId = a.DoctorId,
                    doctorName = a.Doctor.Name,
                    specialty = a.Doctor.Specialty,
                    paymentMethod = a.PaymentMethod,
                    status = a.Status,
                    createdAt = a.CreatedAt,
                })
            });
        }

        [HttpPost]
        [Authorize]
        public IActionResult Book(BookAppointmentDTO input)
        {
            var allowed = new[] { "debit_card", "credit_card", "paypal" };
            if (!allowed.Contains(input.paymentMethod)) return Helper.msg("Payment method not supported.", 422);
            var doctor = dbc.Doctors.FirstOrDefault(d => d.Id == input.doctorId);
            if(doctor == null) return Helper.msg("Doctor not found.", 404);
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            if(input.couponCode != "")
            {
                var coupon = dbc.PromoCodes.FirstOrDefault(pc => pc.Code == input.couponCode);
                if (coupon == null) return Helper.msg("Coupon not found", 404);
                if (DateTime.Now > coupon.ExpiryDate) return Helper.msg("Coupon expired.", 422);
                if (coupon.Quota < 1) return Helper.msg("Coupon quota exceeded.", 422);
                coupon.Quota -= 1;
                dbc.Appointments.Add(new Appointment
                {
                    PatientId = userId,
                    DoctorId = input.doctorId,
                    CouponCode = input.couponCode,
                    PaymentMethod = input.paymentMethod,
                    PricePaid = doctor.Price * (coupon.DiscountPct / 100m),
                });
            } else
            {
                dbc.Appointments.Add(new Appointment
                {
                    PatientId = userId,
                    DoctorId = input.doctorId,
                    CouponCode = input.couponCode,
                    PaymentMethod = input.paymentMethod,
                    PricePaid = doctor.Price,
                });
            }
            dbc.SaveChanges();
            return Helper.msg("Appointment booked successfully.");
        }
    }

    public class BookAppointmentDTO
    {
        [Required] public int doctorId { get; set;  }
        [Required] public string paymentMethod { get; set; } = null!;
        [Required(AllowEmptyStrings = true)]public string couponCode { get; set; } = null!;
    }
}
