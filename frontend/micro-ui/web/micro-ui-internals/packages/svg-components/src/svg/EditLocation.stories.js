import React from "react";
import { EditLocation } from "./EditLocation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EditLocation",
  component: EditLocation,
};

export const Default = () => <EditLocation />;
export const Fill = () => <EditLocation fill="blue" />;
export const Size = () => <EditLocation height="50" width="50" />;
export const CustomStyle = () => <EditLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EditLocation className="custom-class" />;

export const Clickable = () => <EditLocation onClick={()=>console.log("clicked")} />;

const Template = (args) => <EditLocation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
