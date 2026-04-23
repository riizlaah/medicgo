using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

[Index("Code", Name = "UQ__PromoCod__357D4CF91F92233D", IsUnique = true)]
public partial class PromoCode
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("code")]
    [StringLength(50)]
    public string Code { get; set; } = null!;

    [Column("discount_pct", TypeName = "decimal(5, 2)")]
    public decimal DiscountPct { get; set; }

    [Column("quota")]
    public int Quota { get; set; }

    [Column("expiry_date")]
    public DateTime ExpiryDate { get; set; }

    [Column("created_at")]
    public DateTime CreatedAt { get; set; }
}
