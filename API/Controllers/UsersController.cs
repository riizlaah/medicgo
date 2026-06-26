using API.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.ComponentModel.DataAnnotations;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace API.Controllers
{
    [Route("medicgo-api/v1/[controller]")]
    [ApiController]
    public class UsersController : ExtControllerBase
    {
        private readonly MedicGoContext dbc;
        private readonly IConfiguration conf;

        public UsersController(MedicGoContext dbc, IConfiguration conf)
        {
            this.dbc = dbc;
            this.conf = conf;
        }

        [HttpPost("login")]
        public ActionResult login(LoginDTO input)
        {
            var user = dbc.Users.FirstOrDefault(e => e.username == input.username);
            if (user == null || !HashValid(input.password, user.password_hash)) return err("Invalid credentials", 401);
            return json(new
            {
                userId = user.id,
                user.username,
                user.role,
                token = GenToken(user.id, user.role)
            }, "Login successful.");
        }

        [HttpPost("register")]
        public ActionResult register(RegisterDTO input)
        {
            var pw = input.password;
            if (pw.Length < 8) return err("Password length must be 8 characters or more.");
            var hasLetter = pw.Any(Char.IsLetter);
            var hasDigit = pw.Any(Char.IsDigit);
            var hasSymbol = pw.Any(c => !Char.IsLetterOrDigit(c));
            var hasUpper = pw.Any(Char.IsUpper);
            var hasLower = pw.Any(Char.IsLower);
            if (!hasLetter || !hasDigit || !hasSymbol || !hasUpper || !hasLower) return err("Password must contains uppercase & lowercase letter, numbers and symbols.");
            if (!input.phone.All(Char.IsDigit)) return err("Phone number not valid.");
            if (dbc.Users.Any(e => e.username == input.username)) return err("Username has been taken.");
            if (dbc.Users.Any(e => e.email == input.email)) return err("Email has been taken.");
            //if (dbc.Users.Any(e => e.phone == input.phone)) return err("Phone number has been taken.");
            dbc.Users.Add(new User
            {
                username = input.username,
                name = input.fullName,
                email = input.email,
                phone = input.phone,
                role = "patient",
                password_hash = Hash(input.password),
            });
            dbc.SaveChanges();
            return msg("User registered successfully.");
        }


        [HttpGet("profile")]
        [Authorize]
        public ActionResult profile()
        {
            var user = dbc.Users.Find(getUserId());
            if (user == null) return err("User not found");
            return json(new
            {
                userId = user.id,
                username = user.username,
                fullName = user.name,
                email = user.email,
                phone = user.phone,
                role = user.role
            }, "Profile fetched successfully.");
        }


        protected string GenToken(int id, string role)
        {
            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, id.ToString()),
                new Claim(ClaimTypes.Role, role),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            };
            var creds = new SigningCredentials(new SymmetricSecurityKey(Encoding.UTF8.GetBytes(conf["Jwt:Key"])), SecurityAlgorithms.HmacSha256);
            var token = new JwtSecurityToken(conf["Jwt:Issuer"], conf["Jwt:Audience"], claims, expires: DateTime.Now.AddDays(7), signingCredentials: creds);
            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }

    public class LoginDTO
    {
        [Required] public string username { get; set; } = "";
        [Required] public string password { get; set; } = "";
    }

    public class RegisterDTO
    {
        [Required] public string username { get; set; } = "";
        [Required] public string fullName { get; set; } = "";
        [Required][EmailAddress] public string email { get; set; } = "";
        [Required] public string phone { get; set; } = "";
        [Required] public string password { get; set; } = "";
    }
}
