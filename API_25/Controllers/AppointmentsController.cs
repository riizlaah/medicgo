using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/users[controller]")]
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
            return Helper.json(data.Select(a => new
            {
                id = a.Id,
                doctorId = a.DoctorId,
                doctorName = a.Doctor.Name,
                specialty = a.Doctor.Specialty,
                paymentMethod = a.PaymentMethod,
                status = a.Status,
                createdAt = a.CreatedAt,
            }));
        }

        [HttpPost]
        [Authorize]
        public IActionResult Book(BookAppointmentDTO input)
        {
            var allowed = new[] { "debit_card", "credit_card", "paypal" };
            if (!allowed.Contains(input.paymentMethod)) return Helper.err("Payment method not supported");
            var doctor = dbc.Doctors.FirstOrDefault(d => d.Id == input.doctorId);
            if(doctor == null) return Helper.err("Doctor not found");
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            if(input.couponCode != null || input.couponCode != "")
            {
                var coupon = dbc.PromoCodes.FirstOrDefault(pc => pc.Code == input.couponCode);
                if (coupon == null) return Helper.err("Coupon not found");
                if (DateTime.Now > coupon.ExpiryDate) return Helper.err("Coupon expired");
                if (coupon.Quota < 1) return Helper.err("Coupon quota exceeded");
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
            return Helper.json(null);
        }
    }

    public class BookAppointmentDTO
    {
        [Required] public int doctorId { get; set;  }
        [Required] public string paymentMethod { get; set; } = null!;
        public string couponCode { get; set; } = null!;
    }
}
