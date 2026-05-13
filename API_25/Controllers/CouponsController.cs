using API_25.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace API_25.Controllers
{
    [Route("api/coupons")]
    [ApiController]
    public class CouponsController : ControllerBase
    {
        private readonly MedicGoContext dbc;
        public CouponsController(MedicGoContext ctx) { dbc = ctx; }

        [HttpGet("check/{code}")]
        [Authorize]
        public ActionResult Check(string code)
        {
            var coupon = dbc.PromoCodes.FirstOrDefault(c => c.Code == code);
            if (coupon == null) return Helper.msg("Coupon not found", 404);
            if (DateTime.Now > coupon.ExpiryDate) return Helper.msg("Coupon expired", 422);
            if (coupon.Quota < 1) return Helper.msg("Coupon expired", 422);
            return Ok(new
            {
                data = new
                {
                    code = coupon.Code,
                    quota = coupon.Quota,
                    discount = coupon.DiscountPct,
                    expiryDate = coupon.ExpiryDate
                }
            });
        }
    }
}
