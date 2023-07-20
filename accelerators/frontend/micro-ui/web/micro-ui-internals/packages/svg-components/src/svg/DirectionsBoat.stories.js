import React from "react";
import { DirectionsBoat } from "./DirectionsBoat";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsBoat",
  component: DirectionsBoat,
};

export const Default = () => <DirectionsBoat />;
export const Fill = () => <DirectionsBoat fill="blue" />;
export const Size = () => <DirectionsBoat height="50" width="50" />;
export const CustomStyle = () => <DirectionsBoat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsBoat className="custom-class" />;

export const Clickable = () => <DirectionsBoat onClick={()=>console.log("clicked")} />;

const Template = (args) => <DirectionsBoat {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
