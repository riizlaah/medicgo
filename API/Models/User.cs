using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class User
    {
        public int id { get; set; }
        public string name { get; set; } = "";
        public string username { get; set; } = "";
        public string email { get; set; } = "";
        public string password_hash { get; set; } = "";
        public string phone { get; set; } = "";
        public string role { get; set; } = "";
        [Column(TypeName = "datetime2")] public DateTime created_at { get; set; }
        [Column(TypeName = "datetime2")] public DateTime updated_at { get; set; }

        public ICollection<Appointment> appointments { get; set; } = new List<Appointment>();
        public ICollection<SavedDoctor> savedDoctors { get; set; } = new List<SavedDoctor>();
    }
}
