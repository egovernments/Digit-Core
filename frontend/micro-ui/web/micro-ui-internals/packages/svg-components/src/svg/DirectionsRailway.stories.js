import React from "react";
import { DirectionsRailway } from "./DirectionsRailway";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsRailway",
  component: DirectionsRailway,
};

export const Default = () => <DirectionsRailway />;
export const Fill = () => <DirectionsRailway fill="blue" />;
export const Size = () => <DirectionsRailway height="50" width="50" />;
export const CustomStyle = () => <DirectionsRailway style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsRailway className="custom-class" />;

export const Clickable = () => <DirectionsRailway onClick={()=>console.log("clicked")} />;

const Template = (args) => <DirectionsRailway {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
