import React from "react";
import { Flight } from "./Flight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Flight",
  component: Flight,
};

export const Default = () => <Flight />;
export const Fill = () => <Flight fill="blue" />;
export const Size = () => <Flight height="50" width="50" />;
export const CustomStyle = () => <Flight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Flight className="custom-class" />;

export const Clickable = () => <Flight onClick={()=>console.log("clicked")} />;

const Template = (args) => <Flight {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
