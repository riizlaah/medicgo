using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

public partial class Doctor
{
    [Key]
    [Column("id")]
    public int Id { get; set; }

    [Column("name")]
    [StringLength(255)]
    public string Name { get; set; } = null!;

    [Column("specialty")]
    [StringLength(255)]
    public string Specialty { get; set; } = null!;

    [Column("description")]
    public string Description { get; set; } = null!;

    [Column("experience")]
    [StringLength(100)]
    public string Experience { get; set; } = null!;

    [Column("location")]
    [StringLength(255)]
    public string Location { get; set; } = null!;

    [Column("price", TypeName = "decimal(10, 2)")]
    public decimal Price { get; set; }

    [Column("duration")]
    public int Duration { get; set; }

    [Column("created_at")]
    public DateTime CreatedAt { get; set; }

    [Column("updated_at")]
    public DateTime UpdatedAt { get; set; }

    [InverseProperty("Doctor")]
    public virtual ICollection<Appointment> Appointments { get; set; } = new List<Appointment>();

    [InverseProperty("Doctor")]
    public virtual ICollection<Expertise> Expertises { get; set; } = new List<Expertise>();

    [InverseProperty("Doctor")]
    public virtual ICollection<SavedDoctor> SavedDoctors { get; set; } = new List<SavedDoctor>();
}
