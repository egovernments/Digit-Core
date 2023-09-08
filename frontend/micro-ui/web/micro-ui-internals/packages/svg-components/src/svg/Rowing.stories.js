import React from "react";
import { Rowing } from "./Rowing";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Rowing",
  component: Rowing,
};

export const Default = () => <Rowing />;
export const Fill = () => <Rowing fill="blue" />;
export const Size = () => <Rowing height="50" width="50" />;
export const CustomStyle = () => <Rowing style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rowing className="custom-class" />;

export const Clickable = () => <Rowing onClick={()=>console.log("clicked")} />;

const Template = (args) => <Rowing {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
