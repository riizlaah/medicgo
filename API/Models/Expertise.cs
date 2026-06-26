using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class Expertise
    {
        public int id { get; set; }
        
        [Column("doctor_id")] public int doctorId { get; set; }
        public string title { get; set; } = "";
        public string content { get; set; } = "";

        public Doctor doctor = null!;
    }
}
