using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/doctors")]
    [ApiController]
    public class DoctorsController : ControllerBase
    {
        private readonly MedicGoContext dbc;
        public DoctorsController(MedicGoContext ctx) { dbc = ctx; }

        [HttpGet]
        [Authorize]
        public IActionResult GetAll(string search = "", string category = "all", int page = 1, int size = 10, string sort = "desc")
        {
            if (page < 1) return Helper.msg("Page not valid", 422);
            if (size < 1) return Helper.msg("Size not valid", 422);
            var allowedCategories = new[] { "all", "general", "specialist" };
            if (!allowedCategories.Contains(category)) return Helper.msg("Category not valid");
            var query = dbc.Doctors.AsQueryable();
            if(category != "all")
            {
                if(category == "general")
                {
                    query = query.Where(d => d.Specialty == "General Practitioner");
                } else
                {
                    query = query.Where(d => d.Specialty != "General Practitioner");
                }
            }
            if (search != "")
            {
                query = query.Where(d => EF.Functions.Like(d.Specialty, "%" + search + "%") || EF.Functions.Like(d.Name, "%" + search + "%"));
            }
            if(sort =="desc")
            {
                query = query.OrderByDescending(d => d.CreatedAt);
            } else
            {
                query = query.OrderBy(d => d.CreatedAt);
            }
            var totalPages = Convert.ToInt32(Math.Ceiling((decimal)query.Count() / size));
            var data1 = query.Skip((page - 1) * size).Take(size).ToList();
            return Ok(new
            {
                data = data1.Select( d => new
                {
                    id = d.Id,
                    name = d.Name,
                    specialty = d.Specialty,
                    experience = d.Experience,
                    location = d.Location,
                    price = d.Price
                }),
                pagination = new
                {
                    page,
                    size,
                    totalPages
                }
            });
        }

        [Authorize]
        [HttpGet("{id}")]
        public IActionResult Get(int id)
        {
            var d = dbc.Doctors.AsNoTracking().Include(d => d.Expertises).FirstOrDefault(d => d.Id == id);
            if (d == null) return Helper.err("Doctor not found");
            return Ok(new
            {
                data = new
                {
                    id = d.Id,
                    name = d.Name,
                    specialty = d.Specialty,
                    experience = d.Experience,
                    location = d.Location,
                    price = d.Price,
                    description = d.Description,
                    duration = d.Duration,
                    expertise = d.Expertises.Select(e => new
                    {
                        title = e.Title,
                        content = e.Content
                    })
                }
            });
        }

        [HttpPost]
        [Authorize(Roles = "admin")]
        public IActionResult Create(DoctorDTO input)
        {
            if (input.price <= 0m) return Helper.msg("Price must be more than zero.", 422);
            if (input.duration <= 0) return Helper.msg("Duration must be more than zero.", 422);
            if (input.expertises.Length < 3) return Helper.msg("Must include 3 or more expertise/service.", 422);
            if (dbc.Doctors.Any(d => d.Name == input.name)) return Helper.msg("There's already a registered doctor with that name.", 422);
            var newDoctor = input.ToEntity();
            dbc.Doctors.Add(newDoctor);
            dbc.SaveChanges();
            return Helper.msg("Doctor registered successfully.");
        }

        

    }

    public class DoctorDTO
    {
        [Required] public string name { get; set; } = null!;
        [Required] public string specialty { get; set; } = null!;
        [Required] public string description { get; set; } = null!;
        [Required] public string location { get; set; } = null!;
        [Required] public decimal price { get; set; } 
        [Required] public int duration { get; set; }
        [Required] public Expertise[] expertises { get; set; }

        public Doctor ToEntity()
        {
            var doc = new Doctor { Name = name, Specialty = specialty, Description = description, Location = location, Price = price, Duration = duration };
            foreach(var exp in expertises)
            {
                doc.Expertises.Add(new Models.Expertise
                {
                    Title = exp.title,
                    Content = exp.content,
                });
            }
            return doc;
        }

        public Doctor ToEntity(int id)
        {
            return new Doctor { Id = id, Name = name, Specialty = specialty, Description = description, Location = location, Price = price, Duration = duration };
        }
    }
    public class Expertise
    {
        [Required] public string title { get; set; } = null!;
        [Required] public string content { get; set; } = null!;
    }
}

