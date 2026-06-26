using System.ComponentModel.DataAnnotations.Schema;

namespace API.Models
{
    public class PromoCode
    {
        public int id { get; set; }
        public string code { get; set; } = "";
        public decimal discount_pct { get; set; }
        public int quota { get; set; }
        [Column(TypeName = "datetime2")] public DateTime expiry_date { get; set; }
        [Column(TypeName = "datetime2")] public DateTime created_at { get; set; }
    }
}
