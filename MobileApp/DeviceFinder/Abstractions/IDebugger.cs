﻿using System;
using System.Collections.Generic;
using System.Text;

namespace DeviceFinder.Abstractions
{
    public interface IDebugger
    {
        void LogDebugInfo(string text);
    }
}