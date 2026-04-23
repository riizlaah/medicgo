using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace API_25.Models;

public partial class MedicGoContext : DbContext
{
    public MedicGoContext()
    {
    }

    public MedicGoContext(DbContextOptions<MedicGoContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Appointment> Appointments { get; set; }

    public virtual DbSet<Doctor> Doctors { get; set; }

    public virtual DbSet<Expertise> Expertises { get; set; }

    public virtual DbSet<PromoCode> PromoCodes { get; set; }

    public virtual DbSet<SavedDoctor> SavedDoctors { get; set; }

    public virtual DbSet<TokenBlacklist> TokenBlacklists { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseSqlServer("Data Source=(localdb)\\mssqllocaldb;Integrated Security=true;Database=MedicGo");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Appointment>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Appointm__3213E83F74A898C7");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.Status).HasDefaultValue("Waiting for Confirmation");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())");

            entity.HasOne(d => d.Doctor).WithMany(p => p.Appointments)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Appointments_Doctors");

            entity.HasOne(d => d.Patient).WithMany(p => p.Appointments)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Appointments_Users");
        });

        modelBuilder.Entity<Doctor>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Doctors__3213E83F58A02D70");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())");
        });

        modelBuilder.Entity<Expertise>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Expertis__3213E83F5FE3DA26");

            entity.HasOne(d => d.Doctor).WithMany(p => p.Expertises).HasConstraintName("FK_Expertise_Doctors");
        });

        modelBuilder.Entity<PromoCode>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__PromoCod__3213E83F4B18E86A");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");
        });

        modelBuilder.Entity<SavedDoctor>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__SavedDoc__3213E83F51AF0EEF");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");

            entity.HasOne(d => d.Doctor).WithMany(p => p.SavedDoctors)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_SavedDoctors_Doctors");

            entity.HasOne(d => d.Patient).WithMany(p => p.SavedDoctors)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_SavedDoctors_Users");
        });

        modelBuilder.Entity<TokenBlacklist>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__TokenBla__3213E83FE0B88E33");

            entity.Property(e => e.InvalidatedAt).HasDefaultValueSql("(sysutcdatetime())");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Users__3213E83FA3E37978");

            entity.Property(e => e.CreatedAt).HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.UpdatedAt).HasDefaultValueSql("(sysutcdatetime())");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
