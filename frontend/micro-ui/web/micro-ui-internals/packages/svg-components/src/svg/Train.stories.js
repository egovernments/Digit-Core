import React from "react";
import { Train } from "./Train";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Train",
  component: Train,
};

export const Default = () => <Train />;
export const Fill = () => <Train fill="blue" />;
export const Size = () => <Train height="50" width="50" />;
export const CustomStyle = () => <Train style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Train className="custom-class" />;
export const Clickable = () => <Train onClick={()=>console.log("clicked")} />;

const Template = (args) => <Train {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
