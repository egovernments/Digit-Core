import React from "react";
import { ElectricBike } from "./ElectricBike";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricBike",
  component: ElectricBike,
};

export const Default = () => <ElectricBike />;
export const Fill = () => <ElectricBike fill="blue" />;
export const Size = () => <ElectricBike height="50" width="50" />;
export const CustomStyle = () => <ElectricBike style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricBike className="custom-class" />;

export const Clickable = () => <ElectricBike onClick={()=>console.log("clicked")} />;

const Template = (args) => <ElectricBike {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
