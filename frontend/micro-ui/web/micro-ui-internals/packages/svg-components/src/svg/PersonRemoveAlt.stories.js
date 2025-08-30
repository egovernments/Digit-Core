import React from "react";
import { PersonRemoveAlt } from "./PersonRemoveAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonRemoveAlt",
  component: PersonRemoveAlt,
};

export const Default = () => <PersonRemoveAlt />;
export const Fill = () => <PersonRemoveAlt fill="blue" />;
export const Size = () => <PersonRemoveAlt height="50" width="50" />;
export const CustomStyle = () => <PersonRemoveAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonRemoveAlt className="custom-class" />;

export const Clickable = () => <PersonRemoveAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonRemoveAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
