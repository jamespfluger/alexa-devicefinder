﻿using System;
using System.Text;
using System.Threading.Tasks;
using Alexa.NET;
using Alexa.NET.Request;
using Alexa.NET.Response;
using DeviceFinder.AlexaSkill.Services;
using DeviceFinder.AlexaSkill.Utility;
using DeviceFinder.Models;
using OtpNet;

namespace DeviceFinder.AlexaSkill.RequestHandlers
{
    public class AddDeviceHandler : IRequestHandler
    {
        public async Task<SkillResponse> ProcessRequest(Intent request, string alexaUserId)
        {
            try
            {
                string computedOtp = null;
                int numAttempts = 0;

                do
                {
                    computedOtp = GenerateOtp();

                    AlexaUser queriedAuthUser = await DynamoService.Instance.LoadItem<AlexaUser>(computedOtp);

                    if (queriedAuthUser != null)
                    {
                        Logger.Log($"OTP value '{computedOtp}' already exists. Attempting to generate a new one.");
                        numAttempts++;
                        continue;
                    }

                    AlexaUser newAlexaAuthUser = new AlexaUser();
                    newAlexaAuthUser.OneTimePasscode = computedOtp;
                    newAlexaAuthUser.AlexaUserId = alexaUserId;
                    newAlexaAuthUser.TimeToLive = DateTimeOffset.UtcNow.AddMinutes(5).ToUnixTimeSeconds();

                    bool didSaveSucceed = await DynamoService.Instance.SaveItem(newAlexaAuthUser);

                    if (didSaveSucceed)
                        break;
                    else
                        Logger.Log($"Error saving OTP value '{computedOtp}'. Attempting to generate a new one.");

                    numAttempts++;
                } while (numAttempts < 10);

                SsmlOutputSpeech responseMessage = BuildSsmlResponseMessage(computedOtp);

                if (computedOtp != null)
                    return ResponseBuilder.Tell(responseMessage);
                else
                    return ResponseBuilder.Tell($"I'm sorry, I was unable to process your authentication.");
            }
            catch (Exception ex)
            {
                Logger.Log($"Exception handling the authorization intent: {ex}");
                return ResponseBuilder.Tell($"I'm sorry, I was unable to process your authentication. {ex.Message}.");
            }
        }

        private static SsmlOutputSpeech BuildSsmlResponseMessage(string computedOtp)
        {
            SsmlBuilder ssml = new SsmlBuilder();

            ssml.StartSpeech();

            ssml.AddSpeech("Enter this code into the app: ");
            ssml.AddSpeechWithBreaks(computedOtp.ToCharArray(), 500);

            ssml.AddSpeech("Again, the code is: ");
            ssml.AddSpeechWithBreaks(computedOtp.ToCharArray(), 500);

            ssml.AddSpeech("This code will expire in 5 minutes.");

            ssml.EndSpeech();

            return new SsmlOutputSpeech(ssml.Speak());
        }

        private string GenerateOtp()
        {
            byte[] otpKey = Encoding.UTF8.GetBytes(Guid.NewGuid().ToString());
            Totp totp = new Totp(otpKey, 600);

            return totp.ComputeTotp();
        }
    }
}
