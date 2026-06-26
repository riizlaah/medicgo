using API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace API.Controllers
{
    [Route("medicgo-api/v1/[controller]")]
    [ApiController]
    public class DoctorsController : ExtControllerBase
    {
        private readonly MedicGoContext dbc;

        public DoctorsController(MedicGoContext dbc)
        {
            this.dbc = dbc;
        }

        [HttpGet]
        [Authorize]
        public ActionResult getAll(int page = 1, int size = 0, string specialty = "all", string search = "", string sort = "desc")
        {
            if (page <= 0) return err("Page not valid");
            if (size < 0) return err("Size not valid");
            var query = dbc.Doctors.AsQueryable();
            if(specialty != "all")
            {
                if(specialty == "general")
                {
                    query = query.Where(e => EF.Functions.Like(e.specialty, $"%{specialty}%"));
                } else
                {
                    query = query.Where(e => !EF.Functions.Like(e.specialty, $"%{specialty}%"));
                }
            }
            if(search != "")
            {
                query = query.Where(e => EF.Functions.Like(e.name, $"%{search}%") || EF.Functions.Like(e.specialty, $"%{search}%"));
            }
            var count = query.Count();
            if(size > 0)
            {
                query = query.Skip((page - 1) * size).Take(size);
            }
            var data = query.ToList().Select(e => new
            {
                e.id,
                e.name,
                e.specialty,
                e.experience,
                e.location,
                e.price
            });
            return new ObjectResult(new
            {
                data,
                pagination = new
                {
                    page,
                    size,
                    totalPage = size == 0 ? 1 : (int)Math.Ceiling((decimal)count / size)
                }
            });
        }

        [HttpGet("{id}")]
        [Authorize]
        public ActionResult get(int id)
        {
            var e = dbc.Doctors.Include(e => e.expertises).FirstOrDefault(e => e.id == id);
            if (e == null) return err("Doctor not found", 404);
            return json(new
            {
                e.id,
                e.name,
                e.description,
                e.specialty,
                e.experience,
                e.location,
                e.price,
                expertise = e.expertises.ToList().Select(ex => new {ex.title, ex.content})
            }, "Doctor detail fetched successfully");
        }

        [HttpPost()]
        [Authorize]
        public ActionResult create(DoctorDTO input)
        {
            var role = User.FindFirstValue(ClaimTypes.Role) ?? "patient";
            if (role != "admin") return err("Access denied. Admin role required.", 403);
            if (input.expertises.Length < 3) return err("Expertises/Services must be more than 3 items");
            if (input.price <= 0m) return err("Price must be greater than zero");
            if (input.duration <= 0) return err("Price must be greater than zero");
            dbc.Doctors.Add(new Doctor
            {
                name = input.name,
                description = input.description,
                specialty = input.specialty,
                experience = input.experience,
                location = input.location,
                duration = input.duration,
                price = input.price,
                expertises = input.expertises.Select(e => new Expertise { title = e.title, content = e.content }).ToList()
            });
            dbc.SaveChanges();
            return msg("Doctor created successfully.");
        }
    }

    public class DoctorDTO
    {
        [Required] public string name { get; set; }
        [Required] public string description { get; set;}
        [Required] public string specialty { get; set;}
        [Required] public string experience { get; set;}
        [Required] public decimal price { get; set;}
        [Required] public int duration { get; set;}
        [Required] public string location { get; set;}
        [Required] public ExpertiseDTO[] expertises { get; set; }
    }

    public class ExpertiseDTO
    {
        [Required] public string title { get; set; }
        [Required] public string content { get; set; }
    }
}
