﻿using System;
using System.Threading.Tasks;
using Amazon;
using Amazon.DynamoDBv2;
using Amazon.DynamoDBv2.DataModel;
using DeviceFinder.Models.Auth;
using DeviceFinder.Models.Devices;
using Microsoft.AspNetCore.Mvc;

namespace DeviceFinder.DeviceApi.Controllers
{
    /// <summary>
    /// ASP.NET Core controller acting as a DynamoDB Proxy.
    /// </summary>
    [ApiController]
    [Route("devicefinder/[controller]")]
    public class AuthController : Controller
    {
        private readonly DynamoDBContext context;

        public AuthController()
        {
            AmazonDynamoDBClient client = new AmazonDynamoDBClient(RegionEndpoint.USWest2);
            context = new DynamoDBContext(client);
        }

        /// <summary>
        /// Inserts/Updates a Amazon user ID and Android device ID pair
        /// </summary>
        /// <param name="authDevice">Pair of User and Android IDs</param>
        /// TODO: rename this to "devices"
        [HttpPost("users")]
        public async Task<ActionResult> AddNewDevice([FromBody] AuthDevice authDevice)
        {
            try
            {
                // Verify the body we've received has the correct contents
                if (authDevice == null || !authDevice.IsModelValid())
                    return BadRequest($"Error in add: AuthUserDevice body is missing (IsNull={authDevice == null}) or malformed: {authDevice}");

                // Find the user created when interacting with Alexa
                AlexaUser alexaUser = await context.LoadAsync<AlexaUser>(authDevice.OneTimePasscode);

                // Ensure the user exists AND that the OTP has not expired
                if (alexaUser == null)
                    return NotFound();
                else if (alexaUser?.TimeToLive <= DateTimeOffset.UtcNow.ToUnixTimeSeconds())
                    return Unauthorized("The entered code has expired.");

                Device fullUserDevice = new Device
                {
                    AlexaUserId = alexaUser.AlexaUserId,
                    DeviceId = authDevice.DeviceId,
                    LoginUserId = authDevice.LoginUserId,
                    DeviceName = authDevice.DeviceName,
                    DeviceOs = authDevice.DeviceOs
                };

                // Save the new device, and delete the old AlexaUser entry
                Task saveResult = context.SaveAsync(fullUserDevice);
                Task deleteAlexaAuthResult = context.DeleteAsync<AlexaUser>(alexaUser.AlexaUserId);

                Task.WaitAll(saveResult, deleteAlexaAuthResult);

                var result = CreatedAtAction(nameof(AddNewDevice), fullUserDevice);

                return result;
            }
            catch (Exception ex)
            {
                string errorMessage = $"{ex.Message}\n\n{ex}\n\n{authDevice}";
                return BadRequest(errorMessage);
            }
        }
    }
}
