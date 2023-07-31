import React from "react";
import { PersonPinCircle } from "./PersonPinCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonPinCircle",
  component: PersonPinCircle,
};

export const Default = () => <PersonPinCircle />;
export const Fill = () => <PersonPinCircle fill="blue" />;
export const Size = () => <PersonPinCircle height="50" width="50" />;
export const CustomStyle = () => <PersonPinCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonPinCircle className="custom-class" />;

export const Clickable = () => <PersonPinCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonPinCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
