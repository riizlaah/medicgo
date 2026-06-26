using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace API
{
    public class ExtControllerBase: ControllerBase
    {
        protected int getUserId()
        {
            return Convert.ToInt32(User.FindFirstValue(ClaimTypes.NameIdentifier));
        }

        protected ObjectResult json(object? data, string message, int code = 200)
        {
            return new ObjectResult(new { message, data })
            {
                StatusCode = code
            };
        }
        protected ObjectResult msg(string message, int code = 200)
        {
            return new ObjectResult(new { message })
            {
                StatusCode = code
            };
        }

        protected ObjectResult err(string message, int code = 422)
        {
            return new ObjectResult(new { message })
            {
                StatusCode = code
            };
        }

        protected string Hash(string str)
        {
            using(var alg = SHA256.Create())
            {
                var bytes = alg.ComputeHash(Encoding.UTF8.GetBytes(str));
                var sb = new StringBuilder();
                foreach (var b in bytes) sb.Append(b.ToString("x2"));
                return sb.ToString();
            }
        }


        protected bool HashValid(string str, string hashedStr)
        {
            var str2 = Hash(str);
            return StringComparer.OrdinalIgnoreCase.Compare(str2, hashedStr) == 0;
        }
    }
}
