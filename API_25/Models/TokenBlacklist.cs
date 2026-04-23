using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

[Table("TokenBlacklist")]
public partial class TokenBlacklist
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("token")]
    public string Token { get; set; } = null!;

    [Column("invalidated_at")]
    public DateTime InvalidatedAt { get; set; }
}
