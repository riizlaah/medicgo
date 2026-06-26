using API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace API.Controllers
{
    [Route("medicgo-api/v1/[controller]")]
    [ApiController]
    public class AppointmentsController : ExtControllerBase
    {
        private readonly MedicGoContext dbc;

        public AppointmentsController(MedicGoContext dbc)
        {
            this.dbc = dbc;
        }

        [HttpPost]
        [Authorize]
        public ActionResult book(AppointmentBookDTO input)
        {
            var allowed = new[] { "debit_card", "credit_card", "paypal" };
            if (!allowed.Contains(input.paymentMethod)) return err("Payment method not allowed.");
            var doc = dbc.Doctors.Find(input.doctorId);
            if (doc == null) return err("Doctor not found");
            PromoCode? coupon = null;
            var mult = 1m;
            if(input.couponCode != "")
            {
                coupon = dbc.PromoCodes.FirstOrDefault(e => e.code == input.couponCode);
                if (coupon == null) return err("Coupon not found.", 404);
                if (coupon.quota == 0) return err("Coupon quotas exceeded");
                if (coupon.expiry_date < DateTime.Now) return err("Coupon expired");
                coupon.quota -= 1;
                mult = 1m - (coupon.discount_pct * 0.01m);
            }
            dbc.Appointments.Add(new Appointment
            {
                doctorId = input.doctorId,
                patientId = getUserId(),
                coupon_code = coupon?.code,
                payment_method = input.paymentMethod,
                price_paid = doc.price * mult,
            });
            dbc.SaveChanges();
            return msg("Appointment booked.");
        }

        [HttpGet]
        [Authorize]
        public ActionResult getAll()
        {
            var data = dbc.Appointments.Include(e => e.doctor).Where(e => e.patientId == getUserId()).ToList()
                .Select(e => new
                {
                    e.id,
                    e.doctorId,
                    doctorName = e.doctor.name,
                    doctorSpecialty = e.doctor.specialty,
                    paymentMethod = e.payment_method,
                    e.status,
                    createdAt = e.created_at
                });
            return json(data, "Appointments fetched successfully");

        }
    }

    public class AppointmentBookDTO
    {
        [Required] public int doctorId { get; set; }
        [Required] public string paymentMethod { get; set; } = "";
        [Required(AllowEmptyStrings = true)] public string couponCode { get; set; } = "";
    }
}
