using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class Doctor
    {
        public int id { get; set; }
        public string name { get; set; } = "";
        public string specialty { get; set; } = "";
        public string description { get; set; } = "";
        public string experience { get; set; } = "";
        public string location { get; set; } = "";
        public decimal price { get; set; }
        public int duration { get; set; }
        [Column(TypeName = "datetime2")] public DateTime created_at { get; set; }
        [Column(TypeName = "datetime2")] public DateTime updated_at { get; set; }

        public ICollection<Expertise> expertises { get; set; } = new List<Expertise>();
        public ICollection<Appointment> appointments { get; set; } = new List<Appointment>();
        public ICollection<SavedDoctor> savedDoctors { get; set; } = new List<SavedDoctor>();
    }
}
