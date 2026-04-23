using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/[controller]")]
    [ApiController]
    public class DoctorsController : ControllerBase
    {
        private readonly MedicGoContext dbc;
        public DoctorsController(MedicGoContext ctx) { dbc = ctx; }

        [HttpGet]
        [Authorize]
        public IActionResult GetAll(string search = "", int page = 1, int size = 10, string sort = "desc")
        {
            if (page < 1) return Helper.err("Page not valid");
            if (size < 1) return Helper.err("Size not valid");
            var query = dbc.Doctors.AsQueryable();
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
            var data0 = Convert.ToInt32(Math.Floor((decimal)query.Count() / size));
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
                    page = page,
                    size = size,
                    totalPages = data0
                }
            });
        }

        [Authorize]
        [HttpGet("{id}")]
        public IActionResult Get(int id)
        {
            var d = dbc.Doctors.AsNoTracking().Include(d => d.Expertises).FirstOrDefault(d => d.Id == id);
            if (d == null) return Helper.err("Doctor not found");
            return Helper.json(new
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
            });
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

        public Doctor ToEntity()
        {
            return new Doctor { Name = name, Specialty = specialty, Description = description, Location = location, Price = price, Duration = duration };
        }

        public Doctor ToEntity(int id)
        {
            return new Doctor { Id = id, Name = name, Specialty = specialty, Description = description, Location = location, Price = price, Duration = duration };
        }
    }
}
