using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion.Internal;
using Microsoft.VisualBasic;
using System.Diagnostics;
using System.Security.Cryptography;
using System.Text;

namespace API_25
{
    public class Helper
    {

        public static ObjectResult json(object? obj, string msg = "Success", int code = 200)
        {
            return new ObjectResult(new {message = msg, data = obj})
            {
                StatusCode = code
            };
        }

        public static ObjectResult err(string msg, int code = 422)
        {
            return json(null, msg, code);
        }

        public static ObjectResult msg(string msg, int code = 200)
        {
            return new ObjectResult(new { message = msg })
            {
                StatusCode = code
            };
        }

        public static string hash(string text)
        {
            using(var sha256 = SHA256.Create())
            {
                var bytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(text));
                var sb = new StringBuilder();
                foreach(var b in bytes)
                {
                    sb.Append(b.ToString("x2"));
                }
                return sb.ToString();
            }
        }


        public static bool hashValid(string text, string hashedText)
        {
            var hashedT = hash(text);
            Debug.WriteLine(hashedT);
            Debug.WriteLine(hashedText);
            return StringComparer.OrdinalIgnoreCase.Compare(hashedT, hashedText) == 0;
        }
    }
}
