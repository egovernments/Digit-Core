import React from "react";
import { StarRate } from "./StarRate";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StarRate",
  component: StarRate,
};

export const Default = () => <StarRate />;
export const Fill = () => <StarRate fill="blue" />;
export const Size = () => <StarRate height="50" width="50" />;
export const CustomStyle = () => <StarRate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StarRate className="custom-class" />;

export const Clickable = () => <StarRate onClick={()=>console.log("clicked")} />;

const Template = (args) => <StarRate {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
