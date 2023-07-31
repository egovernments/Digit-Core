import React from "react";
import { ElectricScooter } from "./ElectricScooter";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricScooter",
  component: ElectricScooter,
};

export const Default = () => <ElectricScooter />;
export const Fill = () => <ElectricScooter fill="blue" />;
export const Size = () => <ElectricScooter height="50" width="50" />;
export const CustomStyle = () => <ElectricScooter style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricScooter className="custom-class" />;

export const Clickable = () => <ElectricScooter onClick={()=>console.log("clicked")} />;

const Template = (args) => <ElectricScooter {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
