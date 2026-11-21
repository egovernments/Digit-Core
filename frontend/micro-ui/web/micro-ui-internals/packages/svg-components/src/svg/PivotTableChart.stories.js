import React from "react";
import { PivotTableChart } from "./PivotTableChart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PivotTableChart",
  component: PivotTableChart,
};

export const Default = () => <PivotTableChart />;
export const Fill = () => <PivotTableChart fill="blue" />;
export const Size = () => <PivotTableChart height="50" width="50" />;
export const CustomStyle = () => <PivotTableChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PivotTableChart className="custom-class" />;

export const Clickable = () => <PivotTableChart onClick={()=>console.log("clicked")} />;

const Template = (args) => <PivotTableChart {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
