import React from "react";
import { ElectricCar } from "./ElectricCar";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricCar",
  component: ElectricCar,
};

export const Default = () => <ElectricCar />;
export const Fill = () => <ElectricCar fill="blue" />;
export const Size = () => <ElectricCar height="50" width="50" />;
export const CustomStyle = () => <ElectricCar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricCar className="custom-class" />;

export const Clickable = () => <ElectricCar onClick={()=>console.log("clicked")} />;

const Template = (args) => <ElectricCar {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
