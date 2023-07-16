import React from "react";
import { VerifiedUser } from "./VerifiedUser";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VerifiedUser",
  component: VerifiedUser,
};

export const Default = () => <VerifiedUser />;
export const Fill = () => <VerifiedUser fill="blue" />;
export const Size = () => <VerifiedUser height="50" width="50" />;
export const CustomStyle = () => <VerifiedUser style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VerifiedUser className="custom-class" />;
export const Clickable = () => <VerifiedUser onClick={()=>console.log("clicked")} />;

const Template = (args) => <VerifiedUser {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
