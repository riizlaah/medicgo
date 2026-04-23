using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace API_25
{
    public class SecFilter: IOperationFilter
    {
        public void Apply(OpenApiOperation ops, OperationFilterContext ctx)
        {
            var authorized = ctx.MethodInfo.GetCustomAttributes(true)
                .Union(ctx.MethodInfo.GetCustomAttributes(true))
                .OfType<AuthorizeAttribute>()
                .Any();
            if(authorized)
            {
                ops.Security = new List<OpenApiSecurityRequirement>() {
                new OpenApiSecurityRequirement {
                    {
                        new OpenApiSecurityScheme
                        {
                            Name = "Authorization",
                            Scheme = "Bearer",
                            In = ParameterLocation.Header,
                            Type = SecuritySchemeType.Http,
                            Reference = new OpenApiReference
                            {
                                Id = JwtBearerDefaults.AuthenticationScheme,
                                Type = ReferenceType.SecurityScheme
                            }
                        },
                        new List<string>()
                    }
                } }
                ;
            }
        }
    }
}
