import { useState } from "react";

export default function SettingsScreen() {
  const [workDuration, setWorkDuration] = useState(25);
  const [breakDuration, setBreakDuration] = useState(5);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [strictMode, setStrictMode] = useState(false);

  return (
    <div className="min-h-full px-[24px] py-[32px]">
      <div className="mb-[32px]">
        <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] text-[12px] text-[rgba(175,16,26,0.8)] tracking-[1.2px] uppercase mb-[8px]">
          <p className="leading-[18px]">Preferences</p>
        </div>
        <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] text-[#1c1b1f] text-[28px] tracking-[-0.7px]">
          <p className="leading-[42px]">Settings</p>
        </div>
      </div>

      <div className="space-y-[16px]">
        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex items-center justify-between mb-[16px]">
            <div>
              <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f]">
                <p className="leading-[20px]">Work Duration</p>
              </div>
              <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
                <p className="leading-[16px]">{workDuration} minutes</p>
              </div>
            </div>
          </div>
          <input
            type="range"
            min="15"
            max="60"
            step="5"
            value={workDuration}
            onChange={(e) => setWorkDuration(Number(e.target.value))}
            className="w-full h-[6px] bg-[#f1ecf2] rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-[20px] [&::-webkit-slider-thumb]:h-[20px] [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[#AF101A]"
          />
        </div>

        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex items-center justify-between mb-[16px]">
            <div>
              <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f]">
                <p className="leading-[20px]">Break Duration</p>
              </div>
              <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
                <p className="leading-[16px]">{breakDuration} minutes</p>
              </div>
            </div>
          </div>
          <input
            type="range"
            min="3"
            max="15"
            step="1"
            value={breakDuration}
            onChange={(e) => setBreakDuration(Number(e.target.value))}
            className="w-full h-[6px] bg-[#f1ecf2] rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-[20px] [&::-webkit-slider-thumb]:h-[20px] [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[#1B6D24]"
          />
        </div>

        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f]">
                <p className="leading-[20px]">Sound Notifications</p>
              </div>
              <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
                <p className="leading-[16px]">Play sound when timer ends</p>
              </div>
            </div>
            <button
              onClick={() => setSoundEnabled(!soundEnabled)}
              className={`relative w-[48px] h-[28px] rounded-full transition-colors ${
                soundEnabled ? "bg-[#AF101A]" : "bg-[#e4beba]"
              }`}
            >
              <div
                className={`absolute top-[2px] w-[24px] h-[24px] rounded-full bg-white transition-transform ${
                  soundEnabled ? "translate-x-[22px]" : "translate-x-[2px]"
                }`}
              />
            </button>
          </div>
        </div>

        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f]">
                <p className="leading-[20px]">Strict Mode</p>
              </div>
              <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
                <p className="leading-[16px]">Block distracting apps during focus</p>
              </div>
            </div>
            <button
              onClick={() => setStrictMode(!strictMode)}
              className={`relative w-[48px] h-[28px] rounded-full transition-colors ${
                strictMode ? "bg-[#AF101A]" : "bg-[#e4beba]"
              }`}
            >
              <div
                className={`absolute top-[2px] w-[24px] h-[24px] rounded-full bg-white transition-transform ${
                  strictMode ? "translate-x-[22px]" : "translate-x-[2px]"
                }`}
              />
            </button>
          </div>
        </div>

        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f] text-center">
            <p className="leading-[20px]">About TomaFlow</p>
          </div>
          <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)] text-center mt-[8px]">
            <p className="leading-[16px]">Version 1.0.0</p>
            <p className="leading-[16px] mt-[4px]">Built with Material Design 3</p>
          </div>
        </div>
      </div>
    </div>
  );
}
