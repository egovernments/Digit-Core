import React from "react";
import { PersonAddDisabled } from "./PersonAddDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonAddDisabled",
  component: PersonAddDisabled,
};

export const Default = () => <PersonAddDisabled />;
export const Fill = () => <PersonAddDisabled fill="blue" />;
export const Size = () => <PersonAddDisabled height="50" width="50" />;
export const CustomStyle = () => <PersonAddDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAddDisabled className="custom-class" />;

export const Clickable = () => <PersonAddDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonAddDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
