import React from "react";
import { ElectricMoped } from "./ElectricMoped";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricMoped",
  component: ElectricMoped,
};

export const Default = () => <ElectricMoped />;
export const Fill = () => <ElectricMoped fill="blue" />;
export const Size = () => <ElectricMoped height="50" width="50" />;
export const CustomStyle = () => <ElectricMoped style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricMoped className="custom-class" />;

export const Clickable = () => <ElectricMoped onClick={()=>console.log("clicked")} />;

const Template = (args) => <ElectricMoped {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
