using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

[Index("PatientId", "DoctorId", Name = "UQ_SavedDoctors", IsUnique = true)]
public partial class SavedDoctor
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("patient_id")]
    public int PatientId { get; set; }

    [Column("doctor_id")]
    public int DoctorId { get; set; }

    [Column("created_at")]
    public DateTime CreatedAt { get; set; }

    [ForeignKey("DoctorId")]
    [InverseProperty("SavedDoctors")]
    public virtual Doctor Doctor { get; set; } = null!;

    [ForeignKey("PatientId")]
    [InverseProperty("SavedDoctors")]
    public virtual User Patient { get; set; } = null!;
}
