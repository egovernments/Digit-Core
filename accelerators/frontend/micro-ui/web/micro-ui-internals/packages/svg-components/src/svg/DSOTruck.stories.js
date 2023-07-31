import React from "react";
import { DSOTruck } from "./DSOTruck";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DSOTruck",
  component: DSOTruck,
};

export const Default = () => <DSOTruck />;
export const Fill = () => <DSOTruck fill="blue" />;
export const Size = () => <DSOTruck height="50" width="50" />;
export const CustomStyle = () => <DSOTruck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DSOTruck className="custom-class" />;

export const Clickable = () => <DSOTruck onClick={()=>console.log("clicked")} />;

const Template = (args) => <DSOTruck {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
