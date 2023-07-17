import React from "react";
import { DirectionsRun } from "./DirectionsRun";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsRun",
  component: DirectionsRun,
};

export const Default = () => <DirectionsRun />;
export const Fill = () => <DirectionsRun fill="blue" />;
export const Size = () => <DirectionsRun height="50" width="50" />;
export const CustomStyle = () => <DirectionsRun style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsRun className="custom-class" />;

export const Clickable = () => <DirectionsRun onClick={()=>console.log("clicked")} />;

const Template = (args) => <DirectionsRun {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
