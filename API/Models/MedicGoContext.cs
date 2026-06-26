using Microsoft.EntityFrameworkCore;

namespace API.Models
{
    public class MedicGoContext: DbContext
    {
        public DbSet<User> Users { get; set; }
        public DbSet<Doctor> Doctors { get; set; }
        public DbSet<Expertise> Expertise { get; set; }
        public DbSet<PromoCode> PromoCodes { get; set; }
        public DbSet<SavedDoctor> SavedDoctors { get; set; }
        public DbSet<Appointment> Appointments { get; set; }
        public DbSet<TokenBlacklist> TokenBlacklist { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder) => optionsBuilder.UseSqlServer(@"Data Source=(localdb)\mssqllocaldb;Integrated Security=true;Database=MedicGo");

        protected override void OnModelCreating(ModelBuilder builder)
        {
            builder.Entity<Appointment>().HasOne(e => e.patient).WithMany(e => e.appointments);
            builder.Entity<Appointment>().HasOne(e => e.doctor).WithMany(e => e.appointments);
            builder.Entity<Expertise>().HasOne(e => e.doctor).WithMany(e => e.expertises);
            builder.Entity<SavedDoctor>().HasOne(e => e.doctor).WithMany(e => e.savedDoctors);
            builder.Entity<SavedDoctor>().HasOne(e => e.patient).WithMany(e => e.savedDoctors);

            builder.Entity<Appointment>().Property(e => e.created_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<Appointment>().Property(e => e.updated_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<User>().Property(e => e.created_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<User>().Property(e => e.updated_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<Doctor>().Property(e => e.created_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<Doctor>().Property(e => e.updated_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<PromoCode>().Property(e => e.created_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<SavedDoctor>().Property(e => e.created_at).HasDefaultValueSql("(SYSUTCDATETIME())");
            builder.Entity<TokenBlacklist>().Property(e => e.invalidated_at).HasDefaultValueSql("(SYSUTCDATETIME())");
        }
    }
}
