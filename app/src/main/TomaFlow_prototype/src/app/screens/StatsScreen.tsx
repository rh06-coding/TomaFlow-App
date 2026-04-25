import { BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from "recharts";

const weeklyData = [
  { day: "Mon", hours: 3.5, cycles: 7 },
  { day: "Tue", hours: 4.0, cycles: 8 },
  { day: "Wed", hours: 2.5, cycles: 5 },
  { day: "Thu", hours: 5.0, cycles: 10 },
  { day: "Fri", hours: 4.5, cycles: 9 },
  { day: "Sat", hours: 2.0, cycles: 4 },
  { day: "Sun", hours: 1.5, cycles: 3 },
];

export default function StatsScreen() {
  return (
    <div className="min-h-full px-[24px] py-[32px]">
      <div className="mb-[32px]">
        <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] text-[12px] text-[rgba(175,16,26,0.8)] tracking-[1.2px] uppercase mb-[8px]">
          <p className="leading-[18px]">Weekly Progress</p>
        </div>
        <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] text-[#1c1b1f] text-[28px] tracking-[-0.7px]">
          <p className="leading-[42px]">Statistics</p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-[16px] mb-[32px]">
        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[12px] text-[rgba(91,64,61,0.7)] mb-[8px]">
            <p className="leading-[18px]">Total Hours</p>
          </div>
          <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold text-[32px] text-[#AF101A] tracking-[-0.64px]">
            <p className="leading-[40px]">23.0</p>
          </div>
        </div>
        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)]">
          <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[12px] text-[rgba(91,64,61,0.7)] mb-[8px]">
            <p className="leading-[18px]">Total Cycles</p>
          </div>
          <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold text-[32px] text-[#1B6D24] tracking-[-0.64px]">
            <p className="leading-[40px]">46</p>
          </div>
        </div>
      </div>

      <div className="mb-[24px]">
        <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f] mb-[16px]">
          <p className="leading-[20px]">Focus Hours</p>
        </div>
        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)] h-[200px]">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weeklyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(228,190,186,0.2)" />
              <XAxis dataKey="day" tick={{ fill: "#737373", fontSize: 12 }} />
              <YAxis tick={{ fill: "#737373", fontSize: 12 }} />
              <Bar dataKey="hours" fill="#AF101A" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="mb-[24px]">
        <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f] mb-[16px]">
          <p className="leading-[20px]">Pomodoro Cycles</p>
        </div>
        <div className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)] h-[200px]">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weeklyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(228,190,186,0.2)" />
              <XAxis dataKey="day" tick={{ fill: "#737373", fontSize: 12 }} />
              <YAxis tick={{ fill: "#737373", fontSize: 12 }} />
              <Bar dataKey="cycles" fill="#1B6D24" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
