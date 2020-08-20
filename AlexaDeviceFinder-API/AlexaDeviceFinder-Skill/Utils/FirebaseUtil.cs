﻿using System;
using System.Diagnostics;
using System.IO;
using System.Threading.Tasks;
using AlexaDeviceFinderSkill.Models;
using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;

namespace AlexaDeviceFinderSkill
{
    public class FirebaseUtil
    {
        public async Task<string> SendMessage(string token)
        {
            try
            {
                AlexaLambdaEntry.Logger.Log($"Sending notification to {token}");

                // If the default instance hasn't been created, then we need to create it ourselves 
                if (FirebaseApp.DefaultInstance == null)
                    CreateFirebaseApp();

                // This creates a regular notifiation message to be sent
                Message message = CreateNotification(token);

                Stopwatch s = Stopwatch.StartNew();
                string sendResult = await FirebaseMessaging.DefaultInstance.SendAsync(message);
                AlexaLambdaEntry.Logger.LogLine($"Message send time: {s.ElapsedMilliseconds}ms");

                return sendResult;
            }
            catch (Exception ex)
            {
                AlexaLambdaEntry.Logger.Log($"An unhandled exception occured when sending the message for token {token}: {ex}");
                throw;
            }
        }

        private void CreateFirebaseApp()
        {
            try
            {
                AlexaLambdaEntry.Logger.Log("Creating a new instance of the FirebaseApp");
                
                AppOptions appOptions = new AppOptions();
                appOptions.Credential = GoogleCredential.FromFile(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "key.json"));
                FirebaseApp firebaseApp = FirebaseApp.Create(appOptions);
            }
            catch (Exception ex)
            {
                AlexaLambdaEntry.Logger.Log($"Error creating new isntance of FirebaseApp {ex}");
                throw;
            }
        }

        private Message CreateNotification(string token)
        {
            try
            {
                Notification notification = new Notification();
                notification.Title = Constants.NOTIFICATION_TITLE;

                AndroidConfig androidConfig = new AndroidConfig();
                androidConfig = new AndroidConfig();
                androidConfig.Priority = Priority.High;
                androidConfig.Notification = new AndroidNotification();
                androidConfig.Notification.ChannelId = Constants.DEVICE_RING_CHANNEL_ID;

                Message message = new Message();
                message.Notification = notification;
                message.Android = androidConfig;
                message.Token = token;

                return message;
            }
            catch (Exception ex)
            {
                AlexaLambdaEntry.Logger.Log($"Error creating notification for token {token}: {ex}");
                throw;
            }
        }
    }
}
