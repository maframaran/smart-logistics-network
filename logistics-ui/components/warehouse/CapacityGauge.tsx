"use client";

import { RadialBarChart, RadialBar, ResponsiveContainer } from "recharts";
import { fillPercent, gaugeColor } from "@/lib/utils";

interface CapacityGaugeProps {
  label: string;
  current: number;
  max: number;
  unit: string;
}

export function CapacityGauge({ label, current, max, unit }: CapacityGaugeProps) {
  const pct = fillPercent(current, max);
  const color = gaugeColor(pct);
  const fill = color === "red" ? "#ef4444" : color === "amber" ? "#f59e0b" : "#22c55e";

  return (
    <div
      className="flex flex-col items-center gap-1"
      data-testid="capacity-gauge"
      data-color={color}
      aria-valuenow={pct}
      aria-valuemax={100}
    >
      <ResponsiveContainer width={120} height={80}>
        <RadialBarChart
          cx="50%" cy="80%"
          innerRadius="60%" outerRadius="100%"
          startAngle={180} endAngle={0}
          data={[{ value: pct, fill }]}
        >
          <RadialBar dataKey="value" cornerRadius={4} background={{ fill: "#f3f4f6" }} />
        </RadialBarChart>
      </ResponsiveContainer>
      <div className="text-center">
        <p className="text-2xl font-bold" style={{ color: fill }}>{pct}%</p>
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-xs text-muted-foreground">{current.toLocaleString()} / {max.toLocaleString()} {unit}</p>
      </div>
    </div>
  );
}
