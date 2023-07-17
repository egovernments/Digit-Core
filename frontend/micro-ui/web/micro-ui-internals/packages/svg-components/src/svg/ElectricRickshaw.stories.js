import React from "react";
import { ElectricRickshaw } from "./ElectricRickshaw";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricRickshaw",
  component: ElectricRickshaw,
};

export const Default = () => <ElectricRickshaw />;
export const Fill = () => <ElectricRickshaw fill="blue" />;
export const Size = () => <ElectricRickshaw height="50" width="50" />;
export const CustomStyle = () => <ElectricRickshaw style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricRickshaw className="custom-class" />;

export const Clickable = () => <ElectricRickshaw onClick={()=>console.log("clicked")} />;

const Template = (args) => <ElectricRickshaw {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
