using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

[Table("Expertise")]
public partial class Expertise
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("doctor_id")]
    public int DoctorId { get; set; }

    [Column("title")]
    [StringLength(255)]
    public string Title { get; set; } = null!;

    [Column("content")]
    public string Content { get; set; } = null!;

    [ForeignKey("DoctorId")]
    [InverseProperty("Expertises")]
    public virtual Doctor Doctor { get; set; } = null!;
}
