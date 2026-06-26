using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class TokenBlacklist
    {
        public int id { get; set; }

        public string token { get; set; } = "";
        public DateTime invalidated_at { get; set; }
    }
}
