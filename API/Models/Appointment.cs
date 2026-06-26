using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class Appointment
    {
        public int id { get; set; }
        [Column("patient_id")] public int patientId { get; set; }
        [Column("doctor_id")] public int doctorId { get; set; }
        public string payment_method { get; set; } = "";
        public string? coupon_code { get; set; } = "";
        public decimal price_paid { get; set; }
        public string status { get; set; } = "Waiting for Confirmation";
        [Column(TypeName = "datetime2")] public DateTime created_at { get; set; }
        [Column(TypeName = "datetime2")] public DateTime updated_at { get; set; }

        public Doctor doctor = null!;
        public User patient = null!;
    }
}
