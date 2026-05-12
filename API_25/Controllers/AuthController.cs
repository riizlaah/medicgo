using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.ComponentModel.DataAnnotations;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace API_25.Controllers
{
    [Route("medicgo-api/v1/users")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly MedicGoContext dbc;
        private readonly IConfiguration conf;

        public AuthController(MedicGoContext ctx, IConfiguration c)
        {
            dbc = ctx;
            conf = c;
        }


        [HttpPost("login")]
        public IActionResult Login(LoginDTO input)
        {
            var user = dbc.Users.AsNoTracking().FirstOrDefault(u => u.Username == input.username);
            if (user == null)
            {
                return Helper.msg("User not found.", 422);
            }
            if (!Helper.hashValid(input.password, user.PasswordHash))
            {
                return Helper.msg("Wrong username or password.", 422);
            }
            return Helper.json(new
            {
                userId = user.Id,
                username = user.Username,
                role = user.Role,
                token = generateToken(user.Id, user.Role)
            }, "Login successful.");
        }

        [HttpPost("register")]
        public IActionResult Register(RegisterDTO input)
        {
            var hasDigit = input.password.Any(Char.IsDigit);
            var hasLetter = input.password.Any(Char.IsLetter);
            var hasSymbol = input.password.Any(c => !Char.IsLetterOrDigit(c));
            var hasUpper = input.password.Any(Char.IsUpper);
            var hasLower = input.password.Any(Char.IsLower);
            if (!hasDigit || !hasLetter || !hasSymbol || input.password.Length < 8)
            {
                return Helper.msg("Password must contain digit, lowercase and uppercase letter, and symbol and minimum length 8 characters.", 422);
            }
            if(dbc.Users.Any(u => u.Username == input.username))
            {
                return Helper.msg("Username has been used.", 422);
            }
            if (dbc.Users.Any(u => u.Email == input.email))
            {
                return Helper.msg("Email has been used.", 422);
            }
            if (!input.phone.All(Char.IsDigit)) return Helper.msg("Must be digit only", 422);
            dbc.Users.Add(new Models.User {
                Name = input.fullname,
                Username = input.username,
                Email = input.email,
                Phone = input.phone,
                PasswordHash = Helper.hash(input.password),
                Role = "patient"
            });
            dbc.SaveChanges();
            return Helper.msg("User registered successfully.");
        }

        [HttpPost("logout")]
        [Authorize]
        public IActionResult Logout()
        {
            var tokenId = User.FindFirstValue(JwtRegisteredClaimNames.Jti) ?? "";
            if (tokenId == "" || tokenId == null) return Helper.msg("Token not valid", 422);
            if (dbc.TokenBlacklists.Any(t => t.Token == tokenId)) return Helper.msg("Token has been blacklisted.", 422);
            dbc.TokenBlacklists.Add(new TokenBlacklist  
            {
                Token = tokenId,
            });
            dbc.SaveChanges();
            return Helper.msg("Log out successful.");
        }

        [HttpGet("profile")]
        [Authorize]
        public IActionResult Profile()
        {
            var userId = Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
            var user = dbc.Users.First(u => u.Id == userId);
            return Ok(new
            {
                data = new
                {
                    userId = userId,
                    username = user.Username,
                    fullName = user.Name,
                    email = user.Email,
                    phone = user.Phone,
                    role = user.Role,
                }
            });
        }

        private string generateToken(int id, string role)
        {
            var claims = new Claim[]
            {
                new Claim(ClaimTypes.NameIdentifier, id.ToString()),
                new Claim(ClaimTypes.Role, role),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            };
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(conf["Jwt:Key"]));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
            var token = new JwtSecurityToken(conf["Jwt:Issuer"], conf["Jwt:Audience"], claims, expires: DateTime.Now.AddDays(7), signingCredentials: creds);
            return new JwtSecurityTokenHandler().WriteToken(token); 
        }
    }

    public class  LoginDTO
    {
        [Required] public string username { get; set; } = null!;
        [Required] public string password { get; set; } = null!;
    }

    public class RegisterDTO
    {
        [Required] public string fullname { get; set; } = null!;
        [Required] public string username { get; set; } = null!;
        [Required][EmailAddress] public string email { get; set; } = null!;
        [Required] public string phone { get; set; } = null!;
        [Required] public string password { get; set; } = null!;
    }
}
