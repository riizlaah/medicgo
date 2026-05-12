using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/saved-doctors")]
    [ApiController]
    public class SavedDoctorsControllers : ControllerBase
    {
        private readonly MedicGoContext dbc;
        public SavedDoctorsControllers(MedicGoContext ctx) { dbc = ctx; }


        [HttpGet]
        [Authorize]
        public IActionResult GetAll()
        {
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var data = dbc.SavedDoctors.Include(sd => sd.Doctor).Where(sd => sd.PatientId == userId).ToList();
            return Helper.json(data.Select(sd => new
            {
                savedId = sd.Id,
                doctorId = sd.DoctorId,
                doctorName = sd.Doctor.Name,
                specialty = sd.Doctor.Specialty,
                experience = sd.Doctor.Experience,
                location = sd.Doctor.Location,
            }));
        }

        [HttpPost]
        [Authorize]
        public IActionResult Add(SaveDoctorDTO input)
        {
            if (!dbc.Doctors.Any(d => d.Id == input.doctorId)) return Helper.msg("Doctor not found.", 404);
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            if (dbc.SavedDoctors.Any(d => d.DoctorId == input.doctorId && d.PatientId == userId)) return Helper.msg("This doctors has been saved.", 422);
            dbc.SavedDoctors.Add(new SavedDoctor
            {
                DoctorId = input.doctorId,
                PatientId = userId,
            });
            dbc.SaveChanges();
            return Helper.msg("Doctor saved successfully.");
        }

        [HttpDelete("{id}")]
        [Authorize]
        public IActionResult Delete(int id)
        {
            if (!dbc.SavedDoctors.Any(d => d.Id == id)) return Helper.err("Not found", 404);
            var sd = dbc.SavedDoctors.Find(id);
            dbc.SavedDoctors.Remove(sd);
            dbc.SaveChanges();
            return Helper.msg("Saved doctor removed successfully.");
        }

    }

    public class SaveDoctorDTO
    {
        [Required] public int doctorId { get; set; }
    }
}
