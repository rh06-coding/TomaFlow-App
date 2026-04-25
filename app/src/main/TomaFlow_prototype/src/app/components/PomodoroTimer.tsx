import { useState, useEffect } from "react";
import svgPaths from "../../imports/svg-falgfrtn0n";
import { imgBefore } from "../../imports/svg-uqcd0";

export default function PomodoroTimer() {
  const [timeLeft, setTimeLeft] = useState(25 * 60);
  const [isRunning, setIsRunning] = useState(false);
  const totalTime = 25 * 60;

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isRunning && timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            setIsRunning(false);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else if (timeLeft === 0) {
      setIsRunning(false);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRunning, timeLeft]);

  const toggleTimer = () => {
    setIsRunning(!isRunning);
  };

  const resetTimer = () => {
    setIsRunning(false);
    setTimeLeft(totalTime);
  };

  const skipTimer = () => {
    setIsRunning(false);
    setTimeLeft(0);
  };

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const progress = ((totalTime - timeLeft) / totalTime) * 100;
  const circumference = 2 * Math.PI * 138;
  const strokeDashoffset = circumference - (progress / 100) * circumference;

  return (
    <div className="relative size-full">
      <div className="absolute content-stretch flex flex-col items-start left-[121.09px] pb-[48px] top-0">
        <div className="content-stretch flex flex-col gap-[8px] items-start relative shrink-0 w-[149px]">
          <div className="content-stretch flex flex-col items-center relative shrink-0 w-full">
            <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(175,16,26,0.8)] text-center tracking-[1.2px] uppercase whitespace-nowrap">
              <p className="leading-[18px]">Current Session</p>
            </div>
          </div>
          <div className="content-stretch flex flex-col items-center relative shrink-0 w-full">
            <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[28px] text-center tracking-[-0.7px] whitespace-nowrap">
              <p className="leading-[42px]">Focus Time</p>
            </div>
          </div>
        </div>
      </div>

      <div className="absolute content-stretch flex flex-col items-start left-[24px] max-w-[384px] pt-[64px] right-[24px] top-[548px]">
        <div className="bg-white relative rounded-[32px] shrink-0 w-full">
          <div aria-hidden="true" className="absolute border border-[rgba(228,190,186,0.1)] border-solid inset-0 pointer-events-none rounded-[32px]" />
          <div className="flex flex-row items-center size-full">
            <div className="content-stretch flex gap-[16px] items-center p-[25px] relative size-full">
              <div className="bg-[rgba(160,243,153,0.3)] relative rounded-[16px] shrink-0 size-[48px]">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex items-center justify-center relative size-full">
                  <div className="content-stretch flex flex-col items-start relative shrink-0">
                    <div className="flex items-center justify-center relative shrink-0">
                      <div className="-scale-y-100 flex-none">
                        <div className="h-[28px] relative w-[24.02px]">
                          <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24.02 28">
                            <g>
                              <path d={svgPaths.p14944c00} fill="#1B6D24" />
                            </g>
                          </svg>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="relative shrink-0 w-[150px]">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-start relative size-full">
                  <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
                    <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[14px] whitespace-nowrap">
                      <p className="leading-[20px]">Design System Review</p>
                    </div>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0 w-full">
                    <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(91,64,61,0.7)] whitespace-nowrap">
                      <p className="leading-[16px]">Part of Project Sanctuary</p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="flex-[1_0_0] h-[30px] min-w-[24.020000457763672px] relative" />
            </div>
          </div>
        </div>
      </div>

      <div className="absolute content-stretch flex flex-col h-[352px] items-start left-[51px] pb-[64px] top-[116px] w-[288px]">
        <div className="content-stretch flex items-center justify-center pb-[105px] pt-[97px] relative shrink-0 size-[288px]">
          <div className="absolute flex inset-0 items-center justify-center" style={{ containerType: "size" }}>
            <div className="-rotate-90 flex-none h-[100cqw] w-[100cqh]">
              <div className="relative size-full">
                <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 288 288">
                  <g>
                    <path
                      d={svgPaths.p1cddc200}
                      stroke="#FFDAD6"
                      strokeWidth="11.52"
                    />
                    <path
                      d={svgPaths.p1cddc200}
                      stroke="#AF101A"
                      strokeLinecap="round"
                      strokeWidth="11.52"
                      strokeDasharray={circumference}
                      strokeDashoffset={strokeDashoffset}
                      style={{ transition: 'stroke-dashoffset 1s linear' }}
                    />
                  </g>
                </svg>
              </div>
            </div>
          </div>
          <div className="absolute bg-[rgba(175,16,26,0.05)] blur-[12px] right-[-16px] rounded-[9999px] size-[48px] top-[-16px]" />
          <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-[158px]">
            <div className="content-stretch flex flex-col items-center relative shrink-0 w-full">
              <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[56px] text-center tracking-[-1.12px] whitespace-nowrap">
                <p className="leading-[56px]">
                  {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
                </p>
              </div>
            </div>
            <div className="content-stretch flex gap-[8px] items-center justify-center relative shrink-0 w-full">
              <div className="bg-[rgba(175,16,26,0.4)] relative rounded-[8.1px] shrink-0 size-[18px]">
                <div className="-translate-x-1/2 absolute h-[4px] left-1/2 top-[-3px] w-[6px]">
                  <div className="-translate-x-1/2 absolute bg-[#217128] h-[4px] left-1/2 mask-alpha mask-intersect mask-no-clip mask-no-repeat mask-position-[0px_0px] mask-size-[6px_4px] rounded-[1px] top-0 w-[6px]" style={{ maskImage: `url('${imgBefore}')` }} />
                </div>
              </div>
              <div className="content-stretch flex flex-col items-center relative shrink-0">
                <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(91,64,61,0.6)] text-center tracking-[0.6px] uppercase whitespace-nowrap">
                  <p className="leading-[18px]">Deep Work</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="-translate-x-1/2 -translate-y-1/2 absolute content-stretch flex gap-[32px] items-center left-1/2 top-[calc(50%+89px)]">
        <button onClick={resetTimer} className="content-stretch flex flex-col items-center justify-center p-[16px] relative rounded-[9999px] shrink-0">
          <div className="content-stretch flex items-start relative shrink-0">
            <div className="flex items-center justify-center relative shrink-0">
              <div className="-scale-y-100 flex-none">
                <div className="h-[34px] relative w-[28.02px]">
                  <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28.02 34">
                    <g>
                      <path d={svgPaths.p3d12900} fill="#5B403D" />
                    </g>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </button>

        <button onClick={toggleTimer} className="content-stretch flex items-center justify-center relative rounded-[24px] shrink-0 size-[80px]" style={{ backgroundImage: "linear-gradient(135deg, rgb(175, 16, 26) 0%, rgb(211, 47, 47) 100%)" }}>
          <div className="-translate-y-1/2 absolute bg-[rgba(255,255,255,0)] left-0 rounded-[24px] shadow-[0px_20px_25px_-5px_rgba(175,16,26,0.2),0px_8px_10px_-6px_rgba(175,16,26,0.2)] size-[80px] top-1/2" />
          <div className="h-[40px] relative shrink-0 w-[40.02px]">
            <div className="absolute flex h-[48px] items-center justify-center left-0 top-[-4px] w-[40.02px]">
              <div className="-scale-y-100 flex-none">
                <div className="h-[48px] relative w-[40.02px]">
                  <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 40.02 48">
                    <g>
                      {isRunning ? (
                        <>
                          <rect x="12" y="12" width="6" height="24" fill="white" rx="2" />
                          <rect x="22" y="12" width="6" height="24" fill="white" rx="2" />
                        </>
                      ) : (
                        <path d={svgPaths.p17834100} fill="white" />
                      )}
                    </g>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </button>

        <button onClick={skipTimer} className="content-stretch flex flex-col items-center justify-center p-[16px] relative rounded-[9999px] shrink-0">
          <div className="content-stretch flex items-start relative shrink-0">
            <div className="flex items-center justify-center relative shrink-0">
              <div className="-scale-y-100 flex-none">
                <div className="h-[34px] relative w-[28.02px]">
                  <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28.02 34">
                    <g>
                      <path d={svgPaths.p25d569b0} fill="#5B403D" />
                    </g>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </button>
      </div>
    </div>
  );
}
