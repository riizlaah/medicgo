using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class SavedDoctor
    {
        public int id { get; set; }

        [Column("doctor_id")] public int doctorId { get; set; }
        [Column("patient_id")] public int patientId { get; set; }
        [Column(TypeName = "datetime2")] public DateTime created_at { get; set; }

        public Doctor doctor = null!;
        public User patient = null!;
    }
}
