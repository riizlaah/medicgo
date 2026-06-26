using API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace API.Controllers
{
    [Route("medicgo-api/v1/saved-doctors")]
    [ApiController]
    public class SavedDoctorsController : ExtControllerBase
    {
        private readonly MedicGoContext dbc;

        public SavedDoctorsController(MedicGoContext dbc)
        {
            this.dbc = dbc;
        }

        [HttpGet]
        [Authorize]
        public ActionResult getAll()
        {
            var data = dbc.SavedDoctors.Include(e => e.doctor).Where(e => e.patientId == getUserId()).ToList()
                .Select(e => new
                {
                    savedId = e.id,
                    e.doctorId,
                    doctorName = e.doctor.name,
                    e.doctor.specialty,
                    e.doctor.experience,
                    e.doctor.location
                });
            return json(data, "Saved doctors fetched successfuly.");
            
        }

        [HttpPost]
        [Authorize]
        public ActionResult add(SavedDoctorDTO input)
        {
            var userId = getUserId();
            if (!dbc.Doctors.Any(e => e.id == input.doctorId)) return err("Doctor not found.", 404);
            if (dbc.SavedDoctors.Any(e => e.doctorId == input.doctorId && e.patientId == userId)) return err("Doctor already saved.");
            dbc.SavedDoctors.Add(new SavedDoctor
            {
                doctorId = input.doctorId,
                patientId = userId,
            });
            dbc.SaveChanges();
            return msg("Doctor saved successfully.");
        }

        [HttpDelete("{id}")]
        [Authorize]
        public ActionResult delete(int id)
        {
            var e = dbc.SavedDoctors.FirstOrDefault(e => e.id == id && e.patientId == getUserId());
            if (e == null) return err("Saved doctor not found", 404);
            dbc.SavedDoctors.Remove(e);
            dbc.SaveChanges();
            return msg("Saved doctor removed successfully.");
        }
    }

    public class SavedDoctorDTO
    {
        [Required] public int doctorId { get; set; }
    }
}
