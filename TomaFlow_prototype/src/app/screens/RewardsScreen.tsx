import imgUserProfile from "figma:asset/0b9b2c122a9518716e18cb8cb5daf479d52f11ff.png";

const rewardItems = [
  { id: 1, emoji: "🏆", name: "First Focus", unlocked: true },
  { id: 2, emoji: "⭐", name: "Week Warrior", unlocked: true },
  { id: 3, emoji: "🔥", name: "10 Hour Streak", unlocked: true },
  { id: 4, emoji: "💎", name: "Diamond Focus", unlocked: false },
  { id: 5, emoji: "🎯", name: "Perfect Week", unlocked: true },
  { id: 6, emoji: "🌟", name: "Month Master", unlocked: false },
  { id: 7, emoji: "⚡", name: "Speed Demon", unlocked: true },
  { id: 8, emoji: "🎨", name: "Creative Flow", unlocked: false },
  { id: 9, emoji: "🚀", name: "Productivity Pro", unlocked: false },
];

export default function RewardsScreen() {
  return (
    <div className="min-h-full px-[24px] py-[32px]">
      <div className="mb-[32px]">
        <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] text-[12px] text-[rgba(175,16,26,0.8)] tracking-[1.2px] uppercase mb-[8px]">
          <p className="leading-[18px]">Achievements</p>
        </div>
        <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] text-[#1c1b1f] text-[28px] tracking-[-0.7px]">
          <p className="leading-[42px]">Rewards</p>
        </div>
      </div>

      <div className="bg-white rounded-[32px] p-[24px] border border-[rgba(228,190,186,0.1)] mb-[32px]">
        <div className="flex items-center gap-[16px] mb-[20px]">
          <div className="relative rounded-full shrink-0 size-[64px]">
            <div className="content-stretch flex flex-col items-start justify-center overflow-clip p-px relative rounded-[inherit] size-full">
              <div className="flex-1 min-h-px relative w-full">
                <div className="absolute bg-clip-padding border-0 border-transparent border-solid inset-0 overflow-hidden pointer-events-none">
                  <img alt="User profile" className="absolute left-0 max-w-none size-full top-0" src={imgUserProfile} />
                </div>
              </div>
            </div>
            <div aria-hidden="true" className="absolute border border-[rgba(228,190,186,0.2)] border-solid inset-0 pointer-events-none rounded-full" />
          </div>
          <div className="flex-1">
            <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold text-[18px] text-[#1c1b1f] mb-[4px]">
              <p className="leading-[24px]">Focus Master</p>
            </div>
            <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[14px] text-[rgba(91,64,61,0.7)]">
              <p className="leading-[20px]">Level 8 • 127 hours</p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-[12px]">
          <div className="text-center">
            <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold text-[24px] text-[#AF101A] tracking-[-0.48px]">
              <p className="leading-[32px]">127</p>
            </div>
            <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
              <p className="leading-[16px]">Total Hours</p>
            </div>
          </div>
          <div className="text-center">
            <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold text-[24px] text-[#1B6D24] tracking-[-0.48px]">
              <p className="leading-[32px]">254</p>
            </div>
            <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
              <p className="leading-[16px]">Cycles</p>
            </div>
          </div>
          <div className="text-center">
            <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold text-[24px] text-[#AF101A] tracking-[-0.48px]">
              <p className="leading-[32px]">21</p>
            </div>
            <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal text-[12px] text-[rgba(91,64,61,0.7)]">
              <p className="leading-[16px]">Day Streak</p>
            </div>
          </div>
        </div>
      </div>

      <div className="mb-[16px]">
        <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f]">
          <p className="leading-[20px]">Collected Rewards</p>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-[12px]">
        {rewardItems.map((item) => (
          <div
            key={item.id}
            className={`bg-white rounded-[20px] p-[20px] border border-[rgba(228,190,186,0.1)] flex flex-col items-center justify-center ${
              item.unlocked ? "" : "opacity-40"
            }`}
          >
            <div className="text-[40px] mb-[8px]">{item.emoji}</div>
            <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold text-[10px] text-[#1c1b1f] text-center">
              <p className="leading-[14px]">{item.name}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
